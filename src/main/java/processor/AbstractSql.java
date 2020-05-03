package processor;

public abstract class AbstractSql implements Processor{
    private String name;
    public AbstractSql(String name){
        this.name = name;
    }

    @Override
    public void config(){
        System.out.println("Hello " + name);
    }
}
