package xyz.msws.data;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor
public class PostSnapshot {
    @Getter
    @Setter
    private long date;

    @Getter
    @Setter
    private String dateString;

    @Getter
    private int id;
}
