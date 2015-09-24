class TypeErrorTest {
    public static void main(String[] args) {
        System.out.println((new TestClass()).boolMethod());
    }
}

class TestClass {
    public int intMethod() { return 1; }
    public boolean boolMethod() { return true; }
    public boolean boolMethodWithParams(TestClass tc) { return tc; }

    public TestClass tcMethod() {
        TestClass tc;
        int i;
        int[] a;
        boolean b;

        tc = 1;
        tc = true;
        i = tc;
        a = tc;
        a = i;
        i = a;
        b = this.intMethod();
        i = this.boolMethod();
        b = tc;

        tc = new OtherTestClass();
        tc[1] = new TestClass();
        tc[true] = new TestClass();
        a[0] = 1; // OK
        a[0] = true;
        a[0] = tc;
        a[true] = 1;

        b = this.boolMethodWithParams(1);
        b = this.boolMethodWithParams(true);
        i = this.boolMethodWithParams(a);
        a = this.boolMethodWithParams(tc);

        tc = new OtherTestClass();

        a = new int[1]; // OK
        a = new OtherTestClass();
        tc = new int[true];
        b = 1 + 2;
        b = 1 < 2; // OK
        b = true && true; // OK
        b = true < true;

        return 1;
    }
}

class OtherTestClass {

}
