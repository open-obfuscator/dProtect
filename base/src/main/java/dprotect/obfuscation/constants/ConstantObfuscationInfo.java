package dprotect.obfuscation.constants;
import static dprotect.ObfuscationClassSpecification.Level;

public class ConstantObfuscationInfo
{
    public Level level = Level.NONE;

    public ConstantObfuscationInfo(Level level)
    {
        this.level = level;
    }
}
