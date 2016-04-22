/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package glTest.solutions.untexturedObjects.bindlessIndirect;

import glTest.framework.DrawElementsIndirectCommand;

/**
 *
 * @author GBarbieri
 */
public class Command {

    public static final int SIZE = DrawElementsIndirectCommand.SIZE + Integer.BYTES + 3 * BindlessPtrNV.SIZE;
    public static final int OFFSET_DRAW = 0;
    public static final int OFFSET_RESERVED = DrawElementsIndirectCommand.SIZE;
    public static final int OFFSET_INDEX_BUFFER = OFFSET_RESERVED + Integer.BYTES;
    public static final int OFFSET_VERTEX_BUFFER0 = OFFSET_INDEX_BUFFER + BindlessPtrNV.SIZE;
    public static final int OFFSET_VERTEX_BUFFER1 = OFFSET_VERTEX_BUFFER0 + BindlessPtrNV.SIZE;
    
    public DrawElementsIndirectCommand draw;
    public int reserved;
    public BindlessPtrNV indexBuffer;
    // 2x
    public BindlessPtrNV[] vertexBuffers;
}
