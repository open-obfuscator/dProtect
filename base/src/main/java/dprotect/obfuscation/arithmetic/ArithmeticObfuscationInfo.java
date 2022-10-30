package dprotect.obfuscation.arithmetic;
import static dprotect.ObfuscationClassSpecification.Level;

public class ArithmeticObfuscationInfo
{
    public Level   level     = Level.NONE;
    public boolean skipFloat = false;

    public ArithmeticObfuscationInfo(Level level)
    {
        this.level = level;
    }
}
