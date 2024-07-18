package net.cmr.rtd.shader;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.Matrix4;

public class BatchSnapshot {
    
    Matrix4 matrixTemp = new Matrix4();
    int blendSrcFunc;
    int blendDstFunc;
    ShaderProgram shader;

    public BatchSnapshot(Batch batch) {
        matrixTemp.set(batch.getProjectionMatrix());
        blendSrcFunc = batch.getBlendSrcFunc();
        blendDstFunc = batch.getBlendDstFunc();
        shader = batch.getShader();   
    }

    public void restore(Batch batch) {
        batch.setProjectionMatrix(matrixTemp);
        batch.setBlendFunction(blendSrcFunc, blendDstFunc);
        batch.setShader(shader);
    }

    public static BatchSnapshot take(Batch batch) {
        return new BatchSnapshot(batch);
    }

}
