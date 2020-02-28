package line.example.bot.messageapi;

public interface FunctionThrowable<T, R> {

  R apply(T t) throws Exception;
}
