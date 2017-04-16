/*
 * Copyright (c) 2017. Truiton (http://www.truiton.com/).
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Contributors:
 * Mohit Gupt (https://github.com/mohitgupt)
 *
 */

package com.truiton.bottomnavigation;


import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static android.database.sqlite.SQLiteDatabase.openOrCreateDatabase;

public class ItemTwoFragment extends Fragment {

    private RecyclerView mRecyclerView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;

    public static ItemTwoFragment newInstance() {
        ItemTwoFragment fragment = new ItemTwoFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_item_two, container, false);
        return v;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        final Button button = (Button) view.findViewById(R.id.button2);
        final CustomAutoCompleteTextView search = (CustomAutoCompleteTextView) view.findViewById(R.id.autocompletebusNo);

        autocomplete_setup(view, savedInstanceState);

        button.setOnClickListener(
                new Button.OnClickListener() {
                    public void onClick(View v) {
                        if(search.getText() != null){
                            button.setText(search.getText());
                        }
                    }
                }
        );
    }

    public void autocomplete_setup(View view, Bundle savedInstanceState) {
        List<HashMap<String,String>> aList = new ArrayList<HashMap<String,String>>();
        aList = getAllBusNos();
        String[] from = { "flag","txt","route"};
        int[] to = { R.id.flag,R.id.busNo,R.id.route};
        SimpleAdapter adapter = new SimpleAdapter(view.getContext(),aList, R.layout.autocomplete_layout, from, to);
        CustomAutoCompleteTextView autoComplete = ( CustomAutoCompleteTextView) view.findViewById(R.id.autocompletebusNo);
        AdapterView.OnItemClickListener itemClickListener = new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int position, long id) {
                HashMap<String, String> hm = (HashMap<String, String>) arg0.getAdapter().getItem(position);
            }
        };
        autoComplete.setOnItemClickListener(itemClickListener);
        autoComplete.setAdapter(adapter);
    }


    public ArrayList getAllBusNos()
    {

        SQLiteDatabase mydatabase = getActivity().openOrCreateDatabase("WhenBus_db.db", SQLiteDatabase.CREATE_IF_NECESSARY,null);
        Cursor cursor = mydatabase.query("busno_info", new String[] {"busno, src, dest"}, null, null, null, null, null);
        if(cursor.getCount() >0)
        {
            ArrayList list = new ArrayList();
            int i = 0;
            while (cursor.moveToNext())
            {
                String busno = cursor.getString(cursor.getColumnIndex("busno"));
                String src = cursor.getString(cursor.getColumnIndex("src"));
                String dest = cursor.getString(cursor.getColumnIndex("dest"));
                HashMap mMap = new HashMap();
                mMap.put("txt",busno);
                mMap.put("src",src);
                mMap.put("dest",dest);
                mMap.put("route",src+" - "+dest);
                mMap.put("flag",Integer.toString(R.drawable.ic_launcher));
                list.add(mMap);
                i++;
            }
            cursor.close();
            return list;
        }
        else
        {
            cursor.close();
            return new ArrayList();
        }

    }
}
