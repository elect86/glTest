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
    public static final int OFFSET_COUNT = 0;
    public static final int OFFSET_INSTANCE_COUNT = OFFSET_COUNT + Integer.BYTES;
    public static final int OFFSET_FIRST_INDEX = OFFSET_INSTANCE_COUNT + Integer.BYTES;
    public static final int OFFSET_BASE_VERTEX = OFFSET_FIRST_INDEX + Integer.BYTES;
    public static final int OFFSET_BASE_INSTANCE = OFFSET_BASE_VERTEX + Integer.BYTES;

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
