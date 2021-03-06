package module.video.jnc.myffmpeg.opengl;

import android.content.Context;
import android.graphics.ColorSpace;
import android.opengl.GLSurfaceView;
import android.util.Log;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import module.video.jnc.myffmpeg.R;
import android.opengl.GLES20;
import android.opengl.Matrix;
import static android.opengl.GLES20.GL_COLOR_BUFFER_BIT;
import static android.opengl.GLES20.GL_FLOAT;
import static android.opengl.GLES20.GL_LINES;
import static android.opengl.GLES20.GL_LINE_LOOP;
import static android.opengl.GLES20.GL_POINTS;
import static android.opengl.GLES20.GL_TRIANGLES;
import static android.opengl.GLES20.GL_TRIANGLE_FAN;
import static android.opengl.GLES20.glClear;
import static android.opengl.GLES20.glClearColor;
import static android.opengl.GLES20.glDrawArrays;
import static android.opengl.GLES20.glEnableVertexAttribArray;
import static android.opengl.GLES20.glGetAttribLocation;
import static android.opengl.GLES20.glGetUniformLocation;
import static android.opengl.GLES20.glUniform4f;
import static android.opengl.GLES20.glUniformMatrix4fv;
import static android.opengl.GLES20.glUseProgram;
import static android.opengl.GLES20.glVertexAttribPointer;
import static android.opengl.GLES20.glViewport;
/**
 * Created by xhc on 2017/12/7.
 * opengl 是以屏幕中心为坐标原点的。
 */

public class MyRender implements GLSurfaceView.Renderer{

    private Context context;
    private int program ;
//    private static final String U_COLOR = "u_Color";
//    private int uColorLocation ;

    private static final String A_COLOR = "a_Color";
    private int aColorLocation ;


    private static final String A_POSITION = "a_Position";
    private int aPositionLocation ;

    private static final String U_MATRIX = "u_Matrix";
    private int uMatrixLocation ;

    private final float[] projectionMatrix = new float[16];
    private final float[] modelMatrix = new float[16];

    private static final int BYTES_PER_FLOAT = 4;
    private final FloatBuffer vertexData;
    /**
     * opengl会自动把w分量用来做透视除法
     */
//    private static final int POSITION_COMPONENT_COUNT = 4;
//    float[] tableVerticesWithTriangles = {
//            //x , y , z , w , r , g , b
//            0f ,     0f,   0f , 1.5f , 1f , 1f , 1f ,
//            -0.5f,-0.5f,0f , 1f ,  0.7f,0.7f,0.7f,
//            0.5f ,-0.5f,0f , 1f , 0.7f,0.7f,0.7f,
//            0.5f , 0.5f, 0f , 2f ,0.7f,1f,0.7f,
//            -0.5f,0.5f, 0f , 2f , 0.7f,0.7f,0.7f,
//            -0.5f,-0.5f,0f , 1f , 0.7f,0.7f,0.7f,
//            -0.5f ,  0f , 0f , 1.5f , 1f,  0f,  0f,
//             0.5f ,  0f ,  0f , 1.5f ,1f,  0f,  0f,
//               0f , -0.25f ,0f , 1.25f ,0f, 0f,  1f,
//               0f ,  0.25f ,0f , 1.75f ,1f, 0f,  0f};

    private static final int POSITION_COMPONENT_COUNT = 3;
    private static final int COLOR_COMPONENT_COUNT = 3;
    float[] tableVerticesWithTriangles = {
            //x , y ,  r , g , b
            0f ,     0f,  1.5f,  1f , 1f , 1f ,
            -0.5f,-0.5f,   1f,0.7f,0.7f,0.7f,
            0.5f ,-0.5f, 1f, 0.7f,0.7f,0.7f,
            0.5f , 0.5f,  2f ,0.7f,0.7f,0.7f,

            -0.5f,0.5f,   2f , 0.7f,0.7f,0.7f,
            -0.5f,-0.5f,  1f , 0.7f,0.7f,0.7f,

            -0.5f ,  0f ,  1.5f ,  1f,  0f,  0f,
            0.5f ,  0f ,  1.5f ,  1f,  0f,  0f,
            0f , -0.25f ,1.25f ,  0f, 0f,  1f,
            0f ,  0.25f ,1.75f ,  1f, 0f,  0f};

    //数组中因为不全是顶点的坐标，还有颜色等，要告诉opengl中间有多少颜色等。
    private static final int STRIDE = (POSITION_COMPONENT_COUNT + COLOR_COMPONENT_COUNT) * BYTES_PER_FLOAT;

    public MyRender(Context context){
        this.context = context;
        vertexData = ByteBuffer.allocateDirect(tableVerticesWithTriangles.length * BYTES_PER_FLOAT )
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer();
        vertexData.put(tableVerticesWithTriangles);
//        float[] tableVerticec = {0f , 0f , 0f , 14f , 9f , 14f , 9f , 0f};
    }


    @Override
    public void onSurfaceCreated(GL10 gl10, EGLConfig eglConfig) {
        Log.e("xhc" , " render surface create ");
        glClearColor(0.0f , 0.0f ,0.0f,0.0f);
        //加载顶点着色器
        String vertexShaderSource = TextResourceReader.readTextFileFromResource(context , R.raw.simple_vertex_shader);
        //加载片段着色器
        String fragmentShaderSource = TextResourceReader.readTextFileFromResource(context , R.raw.simple_fragment_shader);

        //编译两个程序
        int vertexShader = ShaderHelper.compileVertextShader(vertexShaderSource);
        int fragmentShader = ShaderHelper.compileFragmentShader(fragmentShaderSource);
        Log.e("xhc" , " vertexShader "+vertexShader+" fragmentShader "+fragmentShader);

        if(vertexShader != 0 && fragmentShader != 0){
            //链接到程序
            program = ShaderHelper.linkProgram(vertexShader  ,fragmentShader );
        }

        //验证程序是否可用
        ShaderHelper.validatePrograme(program);
        //使用程序
        glUseProgram(program);
        //找到颜色属性的位置，绘制颜色的时候要使用
//        uColorLocation = glGetUniformLocation(program , U_COLOR);
        aColorLocation = glGetAttribLocation(program , A_COLOR);
        uMatrixLocation = glGetUniformLocation(program , U_MATRIX);
        //找到位置属性的位置
        aPositionLocation = glGetAttribLocation(program , A_POSITION);
        Log.e("xhc" , " aPositionLocation "+aPositionLocation);
        vertexData.position(0);
        /**
         * aPositionLocation 属性的位置，
         * POSITION_COMPONENT_COUNT数据的计数两个，x，y。
         *  GL_FLOAT数据的类型
         */
        glVertexAttribPointer(aPositionLocation , POSITION_COMPONENT_COUNT , GL_FLOAT , false , STRIDE , vertexData);
        glEnableVertexAttribArray(aPositionLocation);

        //读取第一个颜色的时候要跳过前面的位置属性
        vertexData.position(POSITION_COMPONENT_COUNT);
        //调用这个函数就是颜色和着色器中的a_color关联起来，跨距（stride）告诉opengl每个颜色之间有多少个字节
        glVertexAttribPointer(aColorLocation , COLOR_COMPONENT_COUNT , GL_FLOAT , false , STRIDE , vertexData);
        glEnableVertexAttribArray(aColorLocation);
    }

    @Override
    public void onSurfaceChanged(GL10 gl10, int width, int height) {
        glViewport(0 , 0 , width , height);
        //这会用45度的视野创建一个透视投影，这个视椎体从z值-1的位置开始，到z值为-10的位置结束

//        final float aspectRation = width > height ? (float) width / (float) height : (float)height / (float)width;
        /**
         * float[] m, int mOffset, float left, float right, float bottom, float top, float near, float far
         * m:目标数组
         * mOffset：结果矩阵的其实偏移值
         * left: x轴最小的范围
         * right：x轴最大的范围
         * bottom：y轴最小的范围
         * top：y轴最大的范围
         * near：z轴最小的范围
         * far：z轴最大的范围
         * 这个函数就是生成一个正交矩阵，就是将以前在屏幕上的坐标范围（-1，1）改变下e.g（-1.78,1.78）。
         * 如果按上述方式改变下就是相对就更“聚拢”了。
         */
//        Log.e("xhc" , "aspectRation "+aspectRation);
//        if(width > height){
//            Matrix.orthoM(projectionMatrix , 0 ,  -aspectRation , aspectRation ,-1f, 1f , -1f , 1f);
////            Matrix.orthoM(projectionMatrix , 0 ,  -3f , 3f ,-1f, 1f , -1f , 1f);
//        }
//        else{
//            Matrix.orthoM(projectionMatrix , 0 , -1f, 1f , -aspectRation , aspectRation , -1f , 1f);
//        }

        MatrixHelper.perspectiveM(projectionMatrix , 45 , (float)width / (float) height , 1f , 10f);
        Matrix.setIdentityM(modelMatrix , 0);
        //利用模型矩阵移动物体，沿z轴负方向平移-2
        Matrix.translateM(modelMatrix , 0 , 0f , 0f , -2.5f);
        //旋转
//        Matrix.rotateM(modelMatrix , 0 , -60f , 1f , 0f , 0f);

        //投影矩阵乘以模型矩阵。
        final float[] temp = new float[16];
        Matrix.multiplyMM(temp,0 ,projectionMatrix , 0 , modelMatrix , 0);
        System.arraycopy(temp,0,projectionMatrix , 0 , temp.length);
    }

    @Override
    public void onDrawFrame(GL10 gl10) {
        glClear(GL_COLOR_BUFFER_BIT);

        //正交投影
        glUniformMatrix4fv(uMatrixLocation , 1 , false , projectionMatrix , 0);

//        glUniform4f(uColorLocation ,1.0f ,1.0f ,1.0f,1.0f);
        glDrawArrays(GL_TRIANGLE_FAN , 0 , 6);

//        glUniform4f(uColorLocation ,1.0f ,0f ,0f,1.0f);
        glDrawArrays(GL_LINES  , 6 , 2);

//        glUniform4f(uColorLocation ,0.0f ,0.0f ,1.0f,1.0f);
        glDrawArrays(GL_POINTS  , 8 , 1);

//        glUniform4f(uColorLocation ,1.0f ,0.0f ,0.0f,1.0f);
        glDrawArrays(GL_POINTS , 9 , 1);
    }
}
