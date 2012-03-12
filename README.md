The C4J Design by Contract Framework for Java is, since version 4.0, using inheritance to provide classes and interfaces with contracts.

So let's start with some easy code-examples:

Let's assume having a class `CashTerminal` that has to satisfy the contract `CashTerminalContract`:

```java
@Contract(CashTerminalContract.class)
public class CashTerminal {
  public void withdraw(int amount) {
    // withdraw money from customer's bank account
  }
}
```

Our contract `CashTerminalContract` could ensure that only a positive amount of money can be withdrawn from a `CashTerminal`:

```java
public class CashTerminalContract extends CashTerminal {
  @Override
  public void withdraw(final int amount) {
    if (pre()) {
      assert amount > 0;
    }
  }
}
```
