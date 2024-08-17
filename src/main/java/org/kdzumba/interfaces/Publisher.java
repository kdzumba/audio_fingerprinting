package org.kdzumba.interfaces;

import java.util.List;

public interface Publisher {
    void addSubscriber(Subscriber subscriber);
    void removeSubscriber(Subscriber subscriber);
    void notifySubscribers(List<Short> samples);
}
