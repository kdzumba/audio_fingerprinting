package org.kdzumba.interfaces;

import java.util.List;

public interface Subscriber {
    void update(List<Short> samples);
}
