package ru.sberbank.synapse.backstage.serverapi.commons;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class FileResponse {
    String name;
    String uri;
    String type;
    long size;
}
