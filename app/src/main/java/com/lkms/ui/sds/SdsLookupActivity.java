package com.lkms.ui.sds;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.lkms.R;
import com.lkms.data.model.java.Item;
import com.lkms.data.repository.IInventoryRepository;
import com.lkms.data.repository.implement.java.InventoryRepositoryImplJava;
import com.lkms.domain.SdsLookUpUseCase;

import java.util.ArrayList;
import java.util.List;

public class SdsLookupActivity extends AppCompatActivity {

    private SearchView searchView;
    private RecyclerView rvResults;
    private SdsResultAdapter adapter;
    private List<Item> searchResult = new ArrayList<>();

    private SdsLookUpUseCase useCase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sds_lookup);

        searchView = findViewById(R.id.searchView);
        rvResults = findViewById(R.id.rvResults);

        adapter = new SdsResultAdapter(searchResult, item -> fetchSds(item));
        rvResults.setAdapter(adapter);
        rvResults.setLayoutManager(new LinearLayoutManager(this));

        useCase = new SdsLookUpUseCase(new InventoryRepositoryImplJava());

        performSearch("");

        // Search
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                performSearch(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String query) {
                if (query.length() >= 2) performSearch(query);
                return true;
            }
        });
    }

    private void performSearch(String query) {
        useCase.searchItems(query, new SdsLookUpUseCase.InventoryCallback() {
            @Override
            public void onSuccess(List<Item> items) {
                runOnUiThread(() -> {
                    searchResult.clear();
                    searchResult.addAll(items);
                    adapter.notifyDataSetChanged();
                });
            }

            @Override
            public void onError(String errorMessage) {
                runOnUiThread(() ->
                        Toast.makeText(SdsLookupActivity.this, errorMessage, Toast.LENGTH_SHORT).show()
                );
            }
        });
    }

    private void fetchSds(Item item) {
        useCase.getSdsUrl(item.getCasNumber(), new SdsLookUpUseCase.SdsCallback() {
            @Override
            public void onSuccess(String sdsUrl) {
                runOnUiThread(() -> openPdf(sdsUrl));
            }

            @Override
            public void onError(String errorMessage) {
                runOnUiThread(() ->
                        Toast.makeText(SdsLookupActivity.this, errorMessage, Toast.LENGTH_SHORT).show()
                );
            }
        });
    }

    private void openPdf(String url) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setDataAndType(Uri.parse(url), "application/pdf");
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
        startActivity(intent);
    }
}
