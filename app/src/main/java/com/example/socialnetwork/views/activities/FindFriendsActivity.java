package com.example.socialnetwork.views.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;

import com.example.socialnetwork.R;
import com.example.socialnetwork.model.FindFriends;
import com.example.socialnetwork.views.adapters.FindFriendsAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;

public class FindFriendsActivity extends AppCompatActivity {
    private Toolbar mToolbar;

    private ImageButton searchButton;
    private EditText searchInputText;

    private RecyclerView searchResultList;

    private DatabaseReference allUsersDatabaseRef;

    private FindFriendsAdapter findFriendsAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_find_friends);
        init();

        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String searchBoxInput = searchInputText.getText().toString();
                searchPeopleAndFindFriends(searchBoxInput);
            }
        });
    }


    private void init() {
        allUsersDatabaseRef = FirebaseDatabase.getInstance().getReference().child("Users");
        mToolbar = findViewById(R.id.find_friends_appbar_layout);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Find Friends");

        searchButton = findViewById(R.id.search_people_friends_button);
        searchInputText = findViewById(R.id.search_box_input);
        searchResultList = findViewById(R.id.search_result_list);
        searchResultList.setHasFixedSize(true);
        searchResultList.setLayoutManager(new LinearLayoutManager(this));

    }

    private void searchPeopleAndFindFriends(String searchBoxInput) {
        Query searchPeopleAndFriendsQuery = allUsersDatabaseRef.orderByChild("fullname")
                .startAt(searchBoxInput).endAt(searchBoxInput + "\uf8ff");

        final FirebaseRecyclerOptions<FindFriends> options =
                new FirebaseRecyclerOptions.Builder<FindFriends>()
                        .setQuery(searchPeopleAndFriendsQuery, FindFriends.class)
                        .build();

        findFriendsAdapter = new FindFriendsAdapter(options, this, new FindFriendsAdapter.ItemClickListener() {
            @Override
            public void onItemClick(String userKey) {
                Intent personProfileIntent = new Intent(FindFriendsActivity.this, PersonProfileActivity.class);
                System.out.println("----key-----" + userKey);
                personProfileIntent.putExtra("userKey", userKey);
                startActivity(personProfileIntent);
            }
        });

        findFriendsAdapter.startListening();
        searchResultList.setAdapter(findFriendsAdapter);
    }
}
