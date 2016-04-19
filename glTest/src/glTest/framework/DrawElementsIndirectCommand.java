/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package glTest.framework;

/**
 *
 * @author GBarbieri
 */
public class DrawElementsIndirectCommand {

    public static final int SIZE = 5 * Integer.BYTES;
    public static final int OFFSET_COUNT = 0 * Integer.BYTES;
    public static final int OFFSET_INSTANCE_COUNT = 1 * Integer.BYTES;
    public static final int OFFSET_FIRST_INDEX = 2 * Integer.BYTES;
    public static final int OFFSET_BASE_VERTEX = 3 * Integer.BYTES;
    public static final int OFFSET_BASE_INSTANCE = 4 * Integer.BYTES;
    public int count;
    public int instanceCount;
    public int firstIndex;
    public int baseVertex;
    public int baseInstance;

    public DrawElementsIndirectCommand(int count, int instanceCount, int firstIndex, int baseVertex, int baseInstance) {
        this.count = count;
        this.instanceCount = instanceCount;
        this.firstIndex = firstIndex;
        this.baseVertex = baseVertex;
        this.baseInstance = baseInstance;
    }
}
