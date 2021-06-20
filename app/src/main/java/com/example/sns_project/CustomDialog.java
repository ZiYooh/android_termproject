package com.example.sns_project;

import android.app.Dialog;
import android.content.Context;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class CustomDialog {

    private Context context;

    public CustomDialog(Context context){
        this.context = context;
    }
    
    public void callFunction(){ // 매개변수 설정 가능
        final Dialog dlg = new Dialog(context);

        dlg.requestWindowFeature(Window.FEATURE_NO_TITLE);

        dlg.setContentView(R.layout.custom_dialog);

        dlg.show();

        final EditText message = (EditText) dlg.findViewById(R.id.message);
        final Button okButton = (Button) dlg.findViewById(R.id.okButton);
        final Button cancelButton = (Button) dlg.findViewById(R.id.cancelButton);

        okButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                //Toast.makeText(context, "\"" + message.getText().toString() + "\" 을 입력하였습니다.", Toast.LENGTH_SHORT).show();
                dlg.dismiss();
            }
        });
        cancelButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                Toast.makeText(context, "취소 했습니다.", Toast.LENGTH_SHORT).show();

                dlg.dismiss();
            }
        });
    }

}
