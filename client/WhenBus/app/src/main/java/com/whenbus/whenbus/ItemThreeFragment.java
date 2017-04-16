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

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.SimpleAdapter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ItemThreeFragment extends Fragment {
    private RecyclerView mRecyclerView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;

    public static ItemThreeFragment newInstance() {
        ItemThreeFragment fragment = new ItemThreeFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = (View) inflater.inflate(R.layout.fragment_item_three, container, false);
        return v;
    }

    @Override
    public void onViewCreated(final View view, final Bundle savedInstanceState) {
        final Button button = (Button) view.findViewById(R.id.button2);
        final CustomAutoCompleteTextView departure = (CustomAutoCompleteTextView) view.findViewById(R.id.autocomplete_departure);
        final CustomAutoCompleteTextView destination = (CustomAutoCompleteTextView) view.findViewById(R.id.autocomplete_destination);

        autocomplete_setup(view, savedInstanceState);

        button.setOnClickListener(
                new Button.OnClickListener() {
                    public void onClick(View v) {
                        if((departure.getText() != null)&&(destination.getText() != null)){
                            String a = (String) departure.getText().toString();
                            String b = (String) destination.getText().toString();
                            buslist_setup(view,savedInstanceState,a,b);
                        }
                    }
                }
        );
    }

    public void autocomplete_setup(View view, Bundle savedInstanceState) {
        List<HashMap<String,String>> aList = new ArrayList<HashMap<String,String>>();
        aList = getAllstops();
        String[] from = { "flag","txt"};
        int[] to = { R.id.flag,R.id.stop};
        SimpleAdapter adapter = new SimpleAdapter(view.getContext(),aList, R.layout.autocomplete_layout1, from, to);
        CustomAutoCompleteTextView autoComplete1 = ( CustomAutoCompleteTextView) view.findViewById(R.id.autocomplete_departure);
        CustomAutoCompleteTextView autoComplete2 = ( CustomAutoCompleteTextView) view.findViewById(R.id.autocomplete_destination);
        AdapterView.OnItemClickListener itemClickListener = new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int position, long id) {
                HashMap<String, String> hm = (HashMap<String, String>) arg0.getAdapter().getItem(position);
            }
        };
        autoComplete1.setOnItemClickListener(itemClickListener);
        autoComplete1.setAdapter(adapter);
        autoComplete2.setOnItemClickListener(itemClickListener);
        autoComplete2.setAdapter(adapter);

    }

    public ArrayList getAllstops()
    {

        SQLiteDatabase mydatabase = getActivity().openOrCreateDatabase("WhenBus_db.db", SQLiteDatabase.CREATE_IF_NECESSARY,null);
        Cursor cursor = mydatabase.query("busstop_coord", new String[] {"busstop, lat, lng"}, null, null, null, null, null);
        if(cursor.getCount() >0)
        {
            ArrayList list = new ArrayList();
            int i = 0;
            while (cursor.moveToNext())
            {
                String bus_stop = cursor.getString(cursor.getColumnIndex("busstop"));
                String lat = cursor.getString(cursor.getColumnIndex("lat"));
                String lng = cursor.getString(cursor.getColumnIndex("lng"));
                HashMap mMap = new HashMap();
                mMap.put("txt",bus_stop);
                mMap.put("lat",lat);
                mMap.put("lng",lng);
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

    private ArrayList<DataObject> getDataSet() {
        ArrayList results = new ArrayList<DataObject>();
        for (int index = 0; index < 50; index++) {
            DataObject obj = new DataObject("5C" + index,
                    "Taramani"+index, "Broadway" + index, "11:20 AM"+index);
            results.add(index, obj);
        }
        return results;
    }

    public void buslist_setup(View view, Bundle savedInstanceState, String departure, String destination) {
        mRecyclerView = (RecyclerView) view.findViewById(R.id.my_recycler_view);
        mRecyclerView.setHasFixedSize(true);
        mLayoutManager = new LinearLayoutManager(view.getContext());
        mRecyclerView.setLayoutManager(mLayoutManager);
        mAdapter = new MyRecyclerViewAdapter(getDataSet());
        mRecyclerView.setAdapter(mAdapter);
    }
}
