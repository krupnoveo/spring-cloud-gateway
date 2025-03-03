package ru.krupnoveo.edu.gateway.models;

import java.util.List;

public record Service(String name, List<ServiceRoute> routes) {
}
