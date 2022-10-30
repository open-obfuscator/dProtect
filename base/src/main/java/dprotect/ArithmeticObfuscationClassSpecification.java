package dprotect;
import proguard.ClassSpecification;

public class ArithmeticObfuscationClassSpecification extends ObfuscationClassSpecification
{
    public final boolean skipFloat;

    public ArithmeticObfuscationClassSpecification(ClassSpecification classSpecification,
                                                   Level              level,
                                                   boolean            skipFloat)
    {
        super(classSpecification, level);
        this.skipFloat = false;
    }

    @Override
    public boolean equals(Object object)
    {
        if (object == null ||
            this.getClass() != object.getClass())
        {
            return false;
        }

        ArithmeticObfuscationClassSpecification other = (ArithmeticObfuscationClassSpecification)object;
        return
            this.skipFloat == other.skipFloat &&
            super.equals(other);
    }

    @Override
    public int hashCode()
    {
        return
            (skipFloat ? 0 : 1) ^
            super.hashCode();
    }


    @Override
    public Object clone()
    {
        return super.clone();
    }
}

