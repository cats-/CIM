package cats.im.server;

public final class Opcodes {

    public static final short POPUP_MSG = 0;

    public static final short REGISTER = 1;
    public static final short LOGIN = 2;

    public static final short MSG = 3;

    public static final short CHANGE_NAME = 4;
    public static final short CHANGE_STATUS = 5;
    public static final short CHANGE_MOOD = 6;
    public static final short CHANGE_PIC = 7;
    public static final short CHANGE_ACCESS = 8;

    public static final short REQUEST = 9;
    public static final short REQUEST_RESPONSE = 10;

    public static final short ADD_FRIEND = 11;
    public static final short DELETE_FRIEND = 12;
    public static final short INIT = 13;

    private Opcodes(){}
}
