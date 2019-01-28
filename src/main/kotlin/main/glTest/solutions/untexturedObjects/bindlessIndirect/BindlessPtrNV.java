/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package glTest.solutions.untexturedObjects.bindlessIndirect;

/**
 *
 * @author GBarbieri
 */
public class BindlessPtrNV {

    public static final int SIZE = 2 * Integer.BYTES + 2 * Long.BYTES;
    public static final int OFFSET_INDEX = 0;
    public static final int OFFSET_RESERVED = OFFSET_INDEX + Integer.BYTES;
    public static final int OFFSET_ADDRESS = OFFSET_RESERVED + Integer.BYTES;
    public static final int OFFSET_LENGTH = OFFSET_ADDRESS + Long.BYTES;

    public int index;
    public int reserved;
    public long address;
    public long length;
}
