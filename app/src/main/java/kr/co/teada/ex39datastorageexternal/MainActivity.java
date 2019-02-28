package kr.co.teada.ex39datastorageexternal;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

public class MainActivity extends AppCompatActivity {

    EditText et;
    TextView tv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        et=findViewById(R.id.et);
        tv=findViewById(R.id.tv);

    }

    public void clickSave(View view) {

        //1. 외부메모리(SD card) 가 있는지? 확인 ; Environment 사용 지금 니 환경에 외부저장소 있니?
        String state= Environment.getExternalStorageState();

        //2. 외부 메모리 상태가 연결(Mounted)되어 있지 않은지 확인
        if( !state.equals(Environment.MEDIA_MOUNTED)){
            Toast.makeText(this, "SDcard is not Mounted",Toast.LENGTH_SHORT).show();
            return;
        }

        //3. 위에서 return 이 안 됐다는건..연결되어 있으므로 저장작업 시작!
        String data=et.getText().toString();
        et.setText("");

        //4. Data.text라는 이름으로 파일 저장
        //저장될 파일의 디렉토리 경로 지정 필요
        //저장위치 직접 작성 안돼!!  폴더 위치 직접 작성하지말고 경로 받아와야해

        File path; //디렉토리 경로 객체 참조변수

        //5. api 19버전(kitkat) 이상의 폰과 이전의 폰이 코드가 다름
        File[] dirs;
        if(Build.VERSION.SDK_INT>=19){
           dirs= getExternalFilesDirs("MyDir"); //(원래있던거)파일이 저장될 폴더명 지정
        }else{
            //19버전 이상의 API 기능을 사용하기 위해 호환성 버전 클래스(ContextCompat) 이용
           dirs = ContextCompat.getExternalFilesDirs(this, "MyDir"); //(호환용)
        }

        path=dirs[0];
        tv.setText(path.getAbsolutePath());

        //6. 위에 설정된 경로 안에 Data.txt라는 이름의 파일까지 포함시키기
        File file=new File(path, "Data.txt");

        try {
            FileWriter fw=new FileWriter(file, true);  //두 번째 파라미터 : 저장 데이터 추가모드(덮어쓰기 아니고)
            PrintWriter writer=new PrintWriter(fw);

            writer.println(data);
            writer.flush();
            writer.close();

            Toast.makeText(this, "saved", Toast.LENGTH_SHORT).show();

        } catch (IOException e) {
            Toast.makeText(this, "failed", Toast.LENGTH_SHORT).show();
        }


    }

    public void clickLoad(View view) {

        //7.
        String state=Environment.getExternalStorageState();
        if(state.equals(Environment.MEDIA_MOUNTED) || state.equals(Environment.MEDIA_MOUNTED_READ_ONLY)){

            //읽을 수 있는 상태

            File path;
            File[] dirs;

            if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.KITKAT){
                dirs=getExternalFilesDirs("MyDir");
            }else{
                dirs=ContextCompat.getExternalFilesDirs(this, "MyDir");
            }

            path=dirs[0];
            File file=new File(path, "Data.txt");

            try {
                FileReader fr=new FileReader(file);
                BufferedReader reader=new BufferedReader(fr);

                StringBuffer buffer=new StringBuffer();
                String line=reader.readLine();
                while(line!=null){
                    buffer.append(line+"\n");
                    line=reader.readLine();
                }

                tv.setText(buffer.toString());
                reader.close();

            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }


        }

    }

    //8. 동적퍼미션이 필요한 외부 저장소 경로
    //본인 앱 패키지명 폴더(storage/../android/com.teada..) 말고 나머지 다
    public void clickBtn(View view) {
        //이거 먼저 쓰고 시작해
        String state=Environment.getExternalStorageState();

        if( !state.equals(Environment.MEDIA_MOUNTED)){
            Toast.makeText(this,"외부저장소 없음",Toast.LENGTH_SHORT).show();
            return;
        }

        //9. 외부 저장소를 쓰려면 사용자에게 허가(permission)받아야 해
        //Manifest.xml에 퍼미션 사용표시(23버전(Mashmellow) 미만에서는...이것만 하면 돼)

        //위의 방식은 처음 앱을 다운 받을 때 한 번만 퍼미션 사용을 사용자에게 공지!
        //이 방식은 보안 개념에서 문제 있어,(사용자가 뭐가 뭔지도 모르고 다 설치하니까)

        //그래서 사용자가 앱을 실행할 때 마다 물어보도록 (한 번 선택하고 안 물어보게 할 수도 있어)
        //이런 방식을 동적 퍼미션이라고 해. 정적퍼미션은 퍼미션 한 번 써놓고 끝. 요즘은 동적퍼미션이 대세야.
        //ex. 카메라, 연락처, 위치 정도 등..

        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.M){
            //11. 동적퍼미션 작업(이거 조금 이따 할께)---> 다이얼로그로 보여줘서 설정할 수 있게! 일일히 사용자가 직접 설정가서 하게하지말고

            int checkSelfPermission = checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE);
            //퍼미션이 허용되어 있지 않은지
            if(checkSelfPermission==PackageManager.PERMISSION_DENIED){
                //퍼미션을 요청하는 다이얼로그 보여줘

                String[] permissions=new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}; //위에꺼 복붙. 여러개면 , 쓰고 옆에 쭉쭉 써
                requestPermissions(permissions, 100); //100은 식별자 아무거나 쓴거야

                return;
            }

        }

        //10. SDcard의 특정 위치에 저장하기!
        File path=Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
        if(path!=null) tv.setText(path.getAbsolutePath());

        File file=new File(path, "Data.txt");

        try {
            FileWriter fw=new FileWriter(file, true);
            PrintWriter writer=new PrintWriter(fw);

            writer.println(et.getText().toString());
            writer.flush();
            writer.close();

            Toast.makeText(this, "saved", Toast.LENGTH_SHORT).show();


        } catch (IOException e) {
            Toast.makeText(this, "failed", Toast.LENGTH_SHORT).show();
        }

    }//end of clickBtn method

    //12. requestPermissions()메소드로 보여준 다이얼로그 선택이 완료되면 자동으로 실행되는 메소드

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode){
            case 100:

                if(grantResults[0]==PackageManager.PERMISSION_GRANTED){
                    Toast.makeText(this, "외부 저장소 쓰기 가능", Toast.LENGTH_SHORT).show();
                }else
                    Toast.makeText(this, "외부 저장소 쓰기 불가", Toast.LENGTH_SHORT).show();


                break;
        }
    }
}
