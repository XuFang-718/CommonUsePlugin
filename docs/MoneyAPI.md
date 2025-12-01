# CommonUsePlugin Money API 使用文档

## 简介

CommonUsePlugin 提供了 Money API 供其他插件调用，用于查询和操作玩家金钱。

## 依赖配置

### Maven

在你的 `pom.xml` 中添加依赖：

```xml
<dependency>
    <groupId>top.touchstudio</groupId>
    <artifactId>CommonUsePlugin</artifactId>
    <version>1.21.8-0.0.1</version>
    <scope>provided</scope>
</dependency>
```

### plugin.yml

在你的插件 `plugin.yml` 中添加软依赖：

```yaml
softdepend: [CommonUsePlugin]
```

或者硬依赖（如果你的插件必须依赖 Money 功能）：

```yaml
depend: [CommonUsePlugin]
```

## 快速开始

### 获取 API 实例

```java
import top.touchstudio.cup.api.MoneyAPI;

MoneyAPI api = MoneyAPI.getInstance();
```

### 检查模块是否启用

```java
if (api.isEnabled()) {
    // Money 模块已启用，可以使用 API
} else {
    // Money 模块未启用
}
```

## API 方法

### 查询余额

```java
// 通过玩家名查询
int balance = api.getBalance("PlayerName");

// 通过 Player 对象查询
Player player = Bukkit.getPlayer("PlayerName");
int balance = api.getBalance(player);
```

返回值：
- 玩家余额（整数）
- 如果模块未启用，返回 `-1`

### 设置余额

```java
// 通过玩家名设置
boolean success = api.setBalance("PlayerName", 1000);

// 通过 Player 对象设置
boolean success = api.setBalance(player, 1000);
```

参数：
- `playerName` / `player`: 玩家
- `amount`: 金额（必须 >= 0）

返回值：
- `true`: 设置成功
- `false`: 设置失败或模块未启用

### 加钱

```java
// 通过玩家名加钱
boolean success = api.addMoney("PlayerName", 100);

// 通过 Player 对象加钱
boolean success = api.addMoney(player, 100);
```

参数：
- `playerName` / `player`: 玩家
- `amount`: 金额（必须 > 0）

返回值：
- `true`: 操作成功
- `false`: 操作失败或模块未启用

### 扣钱

```java
// 通过玩家名扣钱
boolean success = api.takeMoney("PlayerName", 50);

// 通过 Player 对象扣钱
boolean success = api.takeMoney(player, 50);
```

参数：
- `playerName` / `player`: 玩家
- `amount`: 金额（必须 > 0）

返回值：
- `true`: 扣除成功（余额足够）
- `false`: 余额不足、操作失败或模块未启用

### 检查余额是否足够

```java
// 检查玩家是否有足够的钱
boolean hasEnough = api.hasEnough("PlayerName", 100);

// 通过 Player 对象检查
boolean hasEnough = api.hasEnough(player, 100);
```

参数：
- `playerName` / `player`: 玩家
- `amount`: 需要检查的金额

返回值：
- `true`: 余额 >= amount
- `false`: 余额不足或模块未启用

### 转账

```java
// 通过玩家名转账
boolean success = api.transfer("FromPlayer", "ToPlayer", 100);

// 通过 Player 对象转账
boolean success = api.transfer(fromPlayer, toPlayer, 100);
```

参数：
- `from`: 付款玩家
- `to`: 收款玩家
- `amount`: 转账金额（必须 > 0）

返回值：
- `true`: 转账成功
- `false`: 余额不足、操作失败或模块未启用

### 获取货币信息

```java
// 获取货币名称
String name = api.getCurrencyName();  // 返回 "米"

// 获取货币符号
String symbol = api.getCurrencySymbol();  // 返回 "¥"
```

## 完整示例

### 商店插件示例

```java
package com.example.shop;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import top.touchstudio.cup.api.MoneyAPI;

public class ShopCommand implements CommandExecutor {
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("只有玩家可以使用此命令");
            return true;
        }
        
        MoneyAPI api = MoneyAPI.getInstance();
        
        // 检查 Money 模块是否启用
        if (!api.isEnabled()) {
            player.sendMessage("金钱系统未启用！");
            return true;
        }
        
        int itemPrice = 100;
        
        // 检查余额是否足够
        if (!api.hasEnough(player, itemPrice)) {
            player.sendMessage("余额不足！需要 " + itemPrice + " " + api.getCurrencyName());
            player.sendMessage("当前余额: " + api.getBalance(player) + " " + api.getCurrencyName());
            return true;
        }
        
        // 扣款
        if (api.takeMoney(player, itemPrice)) {
            // 给玩家物品...
            player.sendMessage("购买成功！花费 " + itemPrice + " " + api.getCurrencyName());
            player.sendMessage("剩余余额: " + api.getBalance(player) + " " + api.getCurrencyName());
        } else {
            player.sendMessage("购买失败，请稍后重试");
        }
        
        return true;
    }
}
```

### 奖励插件示例

```java
package com.example.reward;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import top.touchstudio.cup.api.MoneyAPI;

public class DailyRewardListener implements Listener {
    
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        MoneyAPI api = MoneyAPI.getInstance();
        
        if (!api.isEnabled()) return;
        
        // 每日登录奖励
        int reward = 50;
        if (api.addMoney(player, reward)) {
            player.sendMessage("每日登录奖励: +" + reward + " " + api.getCurrencyName());
            player.sendMessage("当前余额: " + api.getBalance(player) + " " + api.getCurrencyName());
        }
    }
}
```

## 注意事项

1. 在调用 API 前，建议先使用 `isEnabled()` 检查模块是否启用
2. 所有金额参数都是整数类型
3. `takeMoney()` 会自动检查余额是否足够，余额不足时返回 `false`
4. 转账操作是原子性的，要么全部成功，要么全部失败
5. API 支持离线玩家操作（通过玩家名）

## 版本兼容性

- Minecraft: 1.20+
- Java: 21+
- Paper API: 1.21.8-R0.1-SNAPSHOT

## 联系方式

- GitHub: https://github.com/TouchStudio
