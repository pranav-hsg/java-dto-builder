
interface Shape{
    public void draw();
}
public class SimpleCoffeeImpl {
    public static void main(String[] args) {
        Coffee simpleCoffee = new SimpleCoffee();
        Coffee coffeeWithMilk = new MilkDecorator(simpleCoffee);

        System.out.println(coffeeWithMilk.getDescription()); // Output: Simple coffee, with milk
        System.out.println(coffeeWithMilk.getCost()); // Output: 1.5
    }
}
