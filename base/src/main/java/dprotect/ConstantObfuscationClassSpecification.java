package dprotect;
import proguard.ClassSpecification;

public class ConstantObfuscationClassSpecification extends ObfuscationClassSpecification
{
    public ConstantObfuscationClassSpecification(ClassSpecification classSpecification,
                                                 Level              level)
    {
        super(classSpecification, level);
    }

    @Override
    public boolean equals(Object object)
    {
        if (object == null ||
            this.getClass() != object.getClass())
        {
            return false;
        }

        ConstantObfuscationClassSpecification other = (ConstantObfuscationClassSpecification)object;
        return super.equals(other);
    }

    @Override
    public int hashCode()
    {
        return super.hashCode();
    }


    @Override
    public Object clone()
    {
        return super.clone();
    }
}

