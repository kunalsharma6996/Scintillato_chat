package com.scintillato.scintillatochat;


import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;


public class Notification extends Fragment {

    private String myJSON,last_fetch_user_id="-1",cur_number;
    private ProgressBar progressBar;
    private Context ctx;
    private boolean last=false;
    private BackGroundTaskRegister backGroundTaskRegister;
    private RecyclerView request_RecyclerView;
    private Notification_Request_Adapter adapter;
    private ArrayList<Notification_Request_List> notification_request_list;

    public Notification() {
        // Required empty public constructor
    }
    @Override
    public View onCreateView(LayoutInflater inflater, final ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view=inflater.inflate(R.layout.notification, container, false);
        ctx=getActivity();
        SharedPreferences sharedpreferences = ctx.getSharedPreferences("User", Context.MODE_PRIVATE);
        cur_number = sharedpreferences.getString("number", "");
        request_RecyclerView=(RecyclerView)view.findViewById(R.id.rv_notification);
        notification_request_list=new ArrayList<>();

        adapter=new Notification_Request_Adapter(ctx,notification_request_list);
        request_RecyclerView.setAdapter(adapter);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getActivity());
        request_RecyclerView.setLayoutManager(mLayoutManager);
        request_RecyclerView.setItemAnimator(new DefaultItemAnimator());

        //fetch_requests
        //progressBar=(ProgressBar)findViewById(R.id.progress_bar_feed);
        return view;
    }



    class BackGroundTaskRegister extends AsyncTask<String, Void, String> {
        int flag1=1;
        int flag;

        BackGroundTaskRegister()
        {
            flag=0;
        }
        @Override
        protected String doInBackground(String... params) {

            String user_id=params[0];
            String last_fetch_user_id=params[1];

            String register_url="http://scintillato.esy.es/fetch_recommend_user.php";

            try{
                URL url=new URL(register_url);
                HttpURLConnection httpURLConnection=(HttpURLConnection)url.openConnection();
                httpURLConnection.setRequestMethod("POST");
                httpURLConnection.setDoOutput(true);
                OutputStream OS=httpURLConnection.getOutputStream();
                BufferedWriter bufferedWriter=new BufferedWriter(new OutputStreamWriter(OS,"UTF-8"));
                String data= URLEncoder.encode("user_id","UTF-8")+"="+URLEncoder.encode(user_id,"UTF-8")+"&"+
                        URLEncoder.encode("last_fetch_user_id","UTF-8")+"="+URLEncoder.encode(last_fetch_user_id,"UTF-8");

                bufferedWriter.write(data);
                bufferedWriter.flush();
                bufferedWriter.close();
                OS.close();
                InputStream IS=httpURLConnection.getInputStream();
                BufferedReader bufferedReader=new BufferedReader(new InputStreamReader(IS,"iso-8859-1"));
                String line="";
                line=bufferedReader.readLine();


                bufferedReader.close();
                IS.close();
                httpURLConnection.disconnect();

                if(line.equals("")==false)
                {
                    flag=1;

                }
                else
                {
                    flag=0;
                }

                return line;

            }
            catch(Exception e)
            {
                flag1=0;
                return "Check Internet Connection!";

            }


        }
        @Override
        protected void onPostExecute(String result) {
            //  Toast.makeText(ctx,result,Toast.LENGTH_LONG);
            progressBar.setVisibility(View.GONE);
            if(flag1==0)
            {
                Toast.makeText(ctx,result,Toast.LENGTH_LONG);
            }
            else
            {

                if(flag==1)
                {
                    //               progressBar.setVisibility(View.GONE);

                    myJSON=result;

                    encode_json(myJSON);
                }
            }
        }

        @Override
        protected void onPreExecute() {
            progressBar.setVisibility(View.VISIBLE);

        }
    }

    private JSONObject jsonObject;
    private JSONArray jsonArray;
    private int count;
    public void encode_json(String myJSON)
    {
        count=0;


        try {
            jsonObject=new JSONObject(myJSON);
            jsonArray=jsonObject.getJSONArray("result");
            String name,fetch_user_id,follower;
            while(count<jsonArray.length())
            {

                JSONObject JO=jsonArray.getJSONObject(count);
                fetch_user_id=JO.getString("fetch_user_id");
                name=JO.getString("user_name");
                follower=JO.getString("followers");
                last=false;
                if (count==jsonArray.length()-1) {
                    if (last_fetch_user_id != fetch_user_id) {
                        last_fetch_user_id = fetch_user_id;
                    }
                    else
                        last=true;
                }

                count++;
            }
        }
        catch (JSONException e)
        {

        }
    }
    @Override
    public void onPause()
    {
        if(backGroundTaskRegister!=null)
        {
            backGroundTaskRegister.cancel(true);
        }
        super.onPause();
    }
    @Override
    public void onStop()
    {
        if(backGroundTaskRegister!=null)
        {
            backGroundTaskRegister.cancel(true);
        }
        super.onStop();
    }

}
