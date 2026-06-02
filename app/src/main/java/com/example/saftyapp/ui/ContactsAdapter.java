package com.example.saftyapp.ui;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.saftyapp.R;
import com.example.saftyapp.data.db.entity.Contact;

import java.util.ArrayList;
import java.util.List;

public class ContactsAdapter extends RecyclerView.Adapter<ContactsAdapter.ContactViewHolder> {
    private List<Contact> contacts = new ArrayList<>();
    private final OnDeleteClickListener deleteClickListener;
    private final OnEditClickListener editClickListener;

    public interface OnDeleteClickListener {
        void onDeleteClick(Contact contact);
    }

    public interface OnEditClickListener {
        void onEditClick(Contact contact);
    }

    public ContactsAdapter(OnDeleteClickListener deleteClickListener, OnEditClickListener editClickListener) {
        this.deleteClickListener = deleteClickListener;
        this.editClickListener = editClickListener;
    }

    public void setContacts(List<Contact> contacts) {
        this.contacts = contacts;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ContactViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_contact, parent, false);
        return new ContactViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ContactViewHolder holder, int position) {
        Contact contact = contacts.get(position);
        holder.tvName.setText(contact.getPriority() + ". " + contact.getName());
        holder.tvPhone.setText(contact.getPhoneNumber());
        holder.btnDelete.setOnClickListener(v -> deleteClickListener.onDeleteClick(contact));
        holder.btnEdit.setOnClickListener(v -> editClickListener.onEditClick(contact));
    }

    @Override
    public int getItemCount() {
        return contacts.size();
    }

    static class ContactViewHolder extends RecyclerView.ViewHolder {
        final TextView tvName;
        final TextView tvPhone;
        final ImageButton btnDelete;
        final ImageButton btnEdit;

        ContactViewHolder(View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvContactName);
            tvPhone = itemView.findViewById(R.id.tvContactPhone);
            btnDelete = itemView.findViewById(R.id.btnDeleteContact);
            btnEdit = itemView.findViewById(R.id.btnEditContact);
        }
    }
}
