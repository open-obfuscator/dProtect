package dprotect;
import proguard.ClassSpecification;


public class ObfuscationClassSpecification extends ClassSpecification
{
    public static enum Level {
        NONE, LOW, MEDIUM, HIGH
    };

    public final Level obfuscationLvl;

    public ObfuscationClassSpecification(ClassSpecification classSpecification,
                                         Level level)
    {
        super(classSpecification);
        this.obfuscationLvl = level;
    }

    public ObfuscationClassSpecification(ClassSpecification classSpecification)
    {
        this(classSpecification, Level.NONE);
    }

    @Override
    public boolean equals(Object object)
    {
        if (object == null ||
            this.getClass() != object.getClass())
        {
            return false;
        }

        ObfuscationClassSpecification other = (ObfuscationClassSpecification)object;
        return
            this.obfuscationLvl == other.obfuscationLvl &&
            super.equals(other);
    }

    @Override
    public int hashCode()
    {
        return obfuscationLvl.hashCode() ^ super.hashCode();
    }

    @Override
    public Object clone()
    {
        return super.clone();
    }
}

