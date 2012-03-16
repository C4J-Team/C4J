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

Let's look at this code example in more detail, step by step.

## Declaring Contracts
A Contract is declared by using the `@Contract` annotation on the _Target_ class or interface, providing the class defining the contract:

```java
@Contract(CashTerminalContract.class)
public class CashTerminal {
  // target class body
}
```

A contract must be satisfied not only by the type for which it is declared, but also by all classes that are extending or implementing this type.

![Inheritance rules for C4J Contracts](/C4J-Team/C4J/raw/master/c4j/doc/inheritance.png)

## Defining Contracts
A contract is defined in a _Contract Class_. This class can extend the _Target Class_ (or implement the _Target Interface_) for convenience and to allow easy refactoring in modern Java IDEs, but doesn't have to (for `final` classes, see chapter below).

```java
public class CashTerminalContract extends CashTerminal {
  // contract class body
}
```

Within the contract class, the methods of the target class can be "overridden", and _Pre-Conditions_ and _Post-Conditions_ can be defined for specific methods. Also, _Class-Invariants_ can be defined.

## Defining Pre- and Post-Conditions
Pre- and Post-Conditions for a method can be defined within the contract class as follows:

```java
@Contract(CashTerminalContract.class)
public class CashTerminal {
  public void withdraw(int amount) {
    // impl
  }
  public int getBalance() {
    // impl
  }
}

public class CashTerminalContract extends CashTerminal {
  @Override
  public void withdraw(final int amount) {
    if (pre()) {
      assert amount > 0;
    }
    if (post()) {
      assert getBalance() == old(getBalance()) - amount;
    }
  }
}
```

The Pre-Condition of `withdraw(int)` requires, that the parameter `amount` is greater than 0. The Post-Condition ensures, that the balance (after execution of method `withdraw(int)`) is equal to the old balance (before execution) substracted by the parameter `amount`.

## old and unchanged
There are two utility methods available, that help defining post-conditions in a short and elegant manner.

`old`, as seen in the example above, can be used to access the value of a field or parameter-less method before the execution of the method in question. There can be no calculations, method-calls or even local variables inside the parameter being passed to `old`.

```java
  assert getBalance() == old(getBalance() - amount); // won't work
  assert getBalance() == old(getBalance()) - old(amount); // won't work
```

As methods should often only modify a particular set of attributes of a class, many post-conditions ensure that the other attributes stay unchanged.

```java
@Contract(TimeOfDayContract.class)
public interface TimeOfDay {
  int getHour();
  int getMinute();
  int getSecond();
  void setHour(int hour);
  void setMinute(int minute);
  void setSecond(int second);
}

public class TimeOfDayContract implements TimeOfDay {
  @Override
  public void setHour(final int hour) {
    if (post()) {
      unchanged(getMinute(), getSecond());
    }
  }
  // etc.
}
```

For primitive types, unchanged checks if the value at the end of the method call is the same as in the beginning of the method call. For reference types, it ensures that the object's state is not being modified while the method is running.

In addition to fields and methods without parameters, method parameters can also be used with `unchanged`. As parameters act as local variables, a redefinition is not being checked and thus unchanged does not have any effect at all, if the method parameter is of primitive type.

## Class-Invariants
Sometimes, a set of conditions can be defined for a class, that always must be satisfied. These conditions can then be defined in a so-called _Class-Invariant_.

```java
@Contract(CashTerminalContract.class)
public class CashTerminal {
  public int getBalance() {
    // impl
  }
}

public class CashTerminalContract extends CashTerminal {
  @ClassInvariant
  public void invariant() {
    assert getBalance() >= 0;
  }
}
```

The class-invariant is introduced by defining a non-overridden method in the contract class, which is annotated by `@ClassInvariant`. The class-invariant will be run after the execution of any non-private method or constructor, enforcing the conditions defined in the class-invariant.

## Final Classes and Methods
Defining contracts for `final` classes and methods can be tricky, as the contract class cannot extend a final target class or override a final method an thus, no refactoring-support is available.

```java
@Contract(FinalClassContract.class)
public final class FinalClass {
  public final void finalMethod() {
    // impl
  }
}

public class FinalClassContract {
  public void finalMethod() {
    // contract impl
  }
}
```

In this example, renaming `finalMethod()` in `FinalClass` would not rename `finalMethod()` in `FinalClassContract` and a warning would be issued on class-loading.

There is one way to avoid the refactoring-trouble: By introducing an interface. Assuming that target and contract class are located within the same package, the interface can even be made package-private. This avoids confusion and hides the synthetic interface from clients. The contract declaration can even move from the final class to the interface.

```java
@Contract(FinalClassInterfaceContract.class)
interface FinalClassInterface {
  void finalMethod();
}

public final class FinalClass implements FinalClassInterface {
  public final void finalMethod() {
    // impl
  }
}

public class FinalClassInterfaceContract implements FinalClassInterface {
  @Override
  public void finalMethod() {
    // contract impl
  }
}
```
