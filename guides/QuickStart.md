# Quick Start
Это руководство поможет подключить Handy и сделать первое окно

## Настройка
Для начала подключите подходящую версию LDLib2

После этого в neoforged.mods.toml
```toml
[[dependencies.${mod_id}]]
modId="handy_wm"
type="required"
versionRange="[1.2.1,)"
ordering="AFTER"
side="BOTH"
```
В build.gradle
```groovy
repositories {
    maven {
        name = 'Handy'
        url = uri('https://vladisfaer.github.io/handy/repository/')
    }
}
dependencies {
    implementation "com.ssc14.handy:window-manager:1.2.1"
}
```

## Первое окно
Для примера можно сделать предмет, у которого можно открыть окно.
Для этого надо наследоваться от класса `ItemUIProvider`
```java
package com.example.examplemod;

import com.lowdragmc.lowdraglib2.gui.ui.ModularUI;
import com.lowdragmc.lowdraglib2.gui.ui.UI;
import com.ssc14.handy.window_manager.api.ItemUIProvider;
import com.ssc14.handy.window_manager.client.gui.GuiMode;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import java.util.UUID;

public class ExampleItem extends ItemUIProvider {
    public ExampleItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        if (level.isClientSide()) {
            openUI(player.getItemInHand(hand), player);
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    public ModularUI getUI(UUID uuid, Player player) {
        UIElement element = new UIElement();
        element.style(style -> style.background("#80FF0000"));
        UI ui = UI.of(element);
        return ModularUI.of(ui, player);
    }
}
```
Здесь 3 метода:
1. `ExampleItem` - обычный конструктор
2. `use` - метод, который вызывается при попытке использовать предмет (то есть нажать им ЛКМ).
В данном случае мы делаем проверку, что это не на сервере и открываем окно (с автоматическим входом в GUI режим)
3. `getUI` - метод, который определяет, из чего состоит окно.
В данном случае он просто создаёт полупрозрачный красный UIElement, из него - UI, а из UI - ModularUI

Результат: полупрозрачный красный прямоугольник
## Параметры окна
Получившееся окно обладает стандартными параметрами окна.
Но параметры этого окна можно изменить. Добавим следующий метод
```java
    public WindowProperties getWindowProperties(){
        return new WindowProperties().width(150).immovable();
    }
```
Теперь наше окно имеет ширину в полтора раза больше, но оно не двигается.
Или вот другой пример:
```java
    public WindowProperties getWindowProperties(){
        return new WindowProperties().minWidth(60).minHeight(60).resizeMargin(5);
    }
```
Теперь окно может изменять размер, но не менее 60 пикселей по вертикали и по горизонтали.
Для изменения размера нужно потянуть за край окна на расстоянии не больше 5 пикселей от края окна

На данный момент существуют следующие параметры:

| **Параметр**  | **Значение по умолчанию** | **Описание**                                |
|---------------|:-------------------------:|---------------------------------------------|
| width         |           `100`           | начальная ширина                            |
| height        |           `100`           | начальная высота                            |
| minWidth      |           `100`           | минимальная ширина                          |
| minHeight     |           `100`           | минимальная высота                          |
| resizeMarginX |            `0`            | отступ для растягивания окна по горизонтали |
| resizeMarginY |            `0`            | отступ для растягивания окна по вертикали   |
| immovable     |          `false`          | недвижимое ли окно                          |
| fullscreen    |          `false`          | полноэкранное окно                          |
| autoSetup     |          `true`           | автоматическая настройка позиционирования   |

## Синхронизация и логика окон
Оконный менеджер Handy автоматически переносит большую часть синхронизации из LDLib2.
Но отличия всё равно имеются. Допустим изначально код окна был такой:
```java
public static ModularUI create(
        HeldItemUIHolder holder
    ) {
    var xml = XmlUtils.loadXml(UI_LOCATION);
    if (xml == null) throw new IllegalStateException("Failed to load " + UI_LOCATION);

    var ui = UI.of(xml);
    
    var counter = ui.selectId("counter")
            .findFirst()
            .orElseThrow();
    var button = ui.selectId("click_button", Button.class)
            .findFirst()
            .orElseThrow();

    ((Label) counter).bind(
            DataBindingBuilder.componentS2C(
                    () -> Component.literal(
                            "Clicks: " + FidgetToyItem.getClicks(holder.itemStack)
                    )
            ).build()
    );
    button.setOnServerClick(event -> {
        FidgetToyItem.incrementClicks(holder.itemStack);
    });

    return ModularUI.of(ui, holder.player);
}
```
Для того чтобы этот код работал в окне нужны следующие изменения:
1. Теперь он принимает `UUID id, Player player` вместо `HeldItemUIHolder holder`
2. Теперь нет холдера, который напрямую дал бы `ItemStack`. Его нужно получать по айди через реестр:
```java
private static int getClicks(UUID id) {
    List<ItemStack> stacks = ServerItemRegistry.INSTANCE.get(id);
    if (stacks.isEmpty()) return 0;
    return FidgetToyItem.getClicks(stacks.getFirst());
}
```
3. Также из-за особенностей реестра событие кнопки надо выполнять на нескольких ItemStack сразу.
Для удобства можно воспользоваться встроенным методом `ServerItemRegistry.execute`
```java
button.setOnServerClick(event ->
    ServerItemRegistry.INSTANCE.execute(id, FidgetToyItem::incrementClicks)
);
```
4. Игрока для построения ModularUI можно передавать напрямую: `return ModularUI.of(ui, player);`

В результате получается такой код:
```java
public static ModularUI create(
        UUID id,
        Player player
    ) {
    var xml = XmlUtils.loadXml(UI_LOCATION);
    if (xml == null) throw new IllegalStateException("Failed to load " + UI_LOCATION);

    var ui = UI.of(xml);
    
    var counter = ui.selectId("counter")
            .findFirst()
            .orElseThrow();
    var button = ui.selectId("click_button", Button.class)
            .findFirst()
            .orElseThrow();

    ((Label) counter).bind(
            DataBindingBuilder.componentS2C(
                    () -> Component.literal(
                            "Clicks: " + getClicks(id)
                    )
            ).build()
    );
    button.setOnServerClick(event ->
            ServerItemRegistry.INSTANCE.execute(id, FidgetToyItem::incrementClicks)
    );

    return ModularUI.of(ui, player);
}
private static int getClicks(UUID id) {
    List<ItemStack> stacks = ServerItemRegistry.INSTANCE.get(id);
    if (stacks.isEmpty()) return 0;
    return FidgetToyItem.getClicks(stacks.getFirst());
}
```
*кстати, это код тестового предмета (Fidget Toy)*
## Постоянные окна
Постоянные окна - это окна, которые открываются сразу при заходе в мир и не закрываются до выхода из мира.
Они могут быть полезны для создания Hud.

Для того чтобы добавить своё постоянное окно, надо зарегистрировать его с помощью события `RegisterPersistentWindowsEvent`
```java
@SubscribeEvent
public static void onPersistentWindowsRegistration(RegisterPersistentWindowsEvent event) {
    event.getManager().registerPersistentWindow(MainMenuOpenHandler::createHotbar);
}
```
Следующий пример создаёт недвижимое окно примерно там же, где находится хотбар:
```java
private static Window createHotbar(){
    var xml = XmlUtils.loadXml(HOTBAR_LOCATION);
    if (xml == null) throw new IllegalStateException("Failed to load " + HOTBAR_LOCATION);
    var ui = UI.of(xml);
    ModularUI mui = ModularUI.of(ui);
    
    Window window = WindowManager.createWindowFromMui(mui, 0, 0, new WindowProperties().autoSetup(false).immovable());

    window.layout(layoutStyle -> layoutStyle
            .positionType(TaffyPosition.ABSOLUTE)
            .width(180)
            .height(20)
            .bottom(3)
            .alignSelf(AlignItems.CENTER)
    );
    
    return window;
}
```
Разберём его по частям:
1. Создаётся `ModularUI` как в обычном окне
2. Вручную создаётся окно из него. Причём автоматическая настройка отключена
3. Настройка положения окна на экране

## Режим GUI
Чтобы игрок мог взаимодействовать с несколькими окнами сразу и одновременно видеть их все,
пока он ими не пользуется, нужно никогда их не закрывать.
Но Screen и HudLayer в Майнкрафте не подразумевают такой возможности.
Поэтому в Handy используется режим GUI.

Пока режим GUI включен, управление камерой ограничено (в этой версии отсутствует),
но игрок может взаимодействовать с окнами.
Пока режим GUI выключен, управление игрока обычное,
но взаимодействия с окнами отключены, окна в таком режиме это просто Hud.

В GuiMode всего 4 метода:

| **Метод** |             **Что он делает**             |
|-----------|:-----------------------------------------:|
| isActive  | Возвращает `boolean` включен ли GUI режим |
| enter     |            Включает GUI режим             |
| exit      |            Выключает GUI режим            |
| toggle    |           Переключает GUI режим           |
