package org.sosalerter.app.ui;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import org.sosalerter.app.data.db.entity.Contact;
import org.sosalerter.app.data.db.entity.EmergencySession;
import org.sosalerter.app.data.repository.EmergencyRepository;

import java.util.List;

public class MainViewModel extends AndroidViewModel {
    private final EmergencyRepository repository;
    private final LiveData<List<Contact>> allContacts;
    private final LiveData<List<EmergencySession>> allSessions;
    private final LiveData<EmergencySession> latestSession;

    public MainViewModel(@NonNull Application application) {
        super(application);
        repository = new EmergencyRepository(application);
        allContacts = repository.getAllContacts();
        allSessions = repository.getAllSessions();
        latestSession = repository.getLatestSession();
    }

    public LiveData<List<Contact>> getAllContacts() {
        return allContacts;
    }

    public LiveData<List<EmergencySession>> getAllSessions() {
        return allSessions;
    }

    public LiveData<EmergencySession> getLatestSession() {
        return latestSession;
    }

    public void insertContact(Contact contact) {
        repository.insertContact(contact);
    }

    public void updateContact(Contact contact) {
        repository.updateContact(contact);
    }

    public void deleteContact(Contact contact) {
        repository.deleteContact(contact);
    }

    public void getSessionDetails(int sessionId, EmergencyRepository.OnSessionDetailsLoadedListener listener) {
        repository.getSessionDetails(sessionId, listener);
    }
}
