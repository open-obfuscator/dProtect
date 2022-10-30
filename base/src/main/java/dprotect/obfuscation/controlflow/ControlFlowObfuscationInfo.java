package dprotect.obfuscation.controlflow;
import static dprotect.ObfuscationClassSpecification.Level;

public class ControlFlowObfuscationInfo
{
    public Level level = Level.NONE;

    public ControlFlowObfuscationInfo(Level level)
    {
        this.level = level;
    }
}
