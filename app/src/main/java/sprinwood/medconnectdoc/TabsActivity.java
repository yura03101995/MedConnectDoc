package sprinwood.medconnectdoc;




import android.app.TabActivity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.TabHost;


public class TabsActivity extends TabActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tabs);

        Intent intent = getIntent();
        String act = "";
        if(getCallingActivity() != null) {
            if (getCallingActivity().getClassName().equals("sprinwood.medconnectdoc.PatientInfoActivity")) {
                act = "2";
            } else
            if(getCallingActivity().getClassName().equals("sprinwood.medconnectdoc.ChatRoomActivity")){
                act = "3";
            }
        }
        TabHost tabHost = getTabHost();
        // инициализация была выполнена в getTabHost
        // метод setup вызывать не нужно

        TabHost.TabSpec tabSpec;

        tabSpec = tabHost.newTabSpec("tag1");
        tabSpec.setIndicator("Профиль");
        tabSpec.setContent(new Intent(this, ProfileActivity.class));
        tabHost.addTab(tabSpec);

        tabSpec = tabHost.newTabSpec("tag2");
        tabSpec.setIndicator("Пациенты");
        tabSpec.setContent(new Intent(this, PatietsActivity.class));
        tabHost.addTab(tabSpec);

        tabSpec = tabHost.newTabSpec("tag3");
        tabSpec.setIndicator("Чаты");
        tabSpec.setContent(new Intent(this, ChatsActicity.class));
        tabHost.addTab(tabSpec);


        if(act.equals("2")){
            tabHost.setCurrentTab(1);
        } else if(act.equals("3")){
            tabHost.setCurrentTab(2);
        }
    }

}
