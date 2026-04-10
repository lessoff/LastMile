package com.delivery.optimizer.algorithm;

import com.delivery.optimizer.model.DeliveryGraph;
import com.delivery.optimizer.model.Package;
import com.delivery.optimizer.model.Route;

import java.util.List;

public interface RoutingStrategy {
    Route optimize(DeliveryGraph graph, List<Package> packages);
    String getName();
}
