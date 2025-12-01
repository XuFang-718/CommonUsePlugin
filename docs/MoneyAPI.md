# Money API

## 获取实例

```java
MoneyAPI api = MoneyAPI.getInstance();
```

## API 方法

| 方法 | 参数 | 返回值 | 说明 |
|------|------|--------|------|
| `isEnabled()` | - | `boolean` | 检查模块是否启用 |
| `getBalance(String playerName)` | 玩家名 | `int` | 获取余额，未启用返回 -1 |
| `getBalance(Player player)` | 玩家对象 | `int` | 获取余额 |
| `setBalance(String playerName, int amount)` | 玩家名, 金额(>=0) | `boolean` | 设置余额 |
| `setBalance(Player player, int amount)` | 玩家对象, 金额 | `boolean` | 设置余额 |
| `addMoney(String playerName, int amount)` | 玩家名, 金额(>0) | `boolean` | 加钱 |
| `addMoney(Player player, int amount)` | 玩家对象, 金额 | `boolean` | 加钱 |
| `takeMoney(String playerName, int amount)` | 玩家名, 金额(>0) | `boolean` | 扣钱，余额不足返回 false |
| `takeMoney(Player player, int amount)` | 玩家对象, 金额 | `boolean` | 扣钱 |
| `hasEnough(String playerName, int amount)` | 玩家名, 金额 | `boolean` | 检查余额是否足够 |
| `hasEnough(Player player, int amount)` | 玩家对象, 金额 | `boolean` | 检查余额是否足够 |
| `transfer(String from, String to, int amount)` | 付款人, 收款人, 金额 | `boolean` | 转账 |
| `transfer(Player from, Player to, int amount)` | 付款人, 收款人, 金额 | `boolean` | 转账 |
| `getCurrencyName()` | - | `String` | 返回 "枚硬币" |
| `getCurrencySymbol()` | - | `String` | 返回 "¥" |

## 示例

```java
MoneyAPI api = MoneyAPI.getInstance();

if (api.isEnabled()) {
    // 查询余额
    int balance = api.getBalance("PlayerName");
    
    // 加钱
    api.addMoney("PlayerName", 100);
    
    // 扣钱（会检查余额）
    if (api.takeMoney("PlayerName", 50)) {
        // 扣款成功
    }
    
    // 转账
    api.transfer("Player1", "Player2", 100);
}
```
