package no.uib.inf273.data

data class Cargo(
    val index: Int,
    val origin_port: Int,
    val destPort: Int,
    val size: Int,
    val ntCost: Int,
    val lowerPickup: Int,
    val upperPickup: Int,
    val lowerDelivery: Int,
    val upperDelivery: Int
) {

//    param origin_port {C} >= 0, <= ports;   # Origin port
//    param dest_port {C} >= 0, <= ports;     # Destination port
//    param size {C} >= 0;                    # Size of cargo
//    param nt_cost {C} >= 0;                 # Cost of not transporting (math: C)
//    param lower_pickup {C} >= 0;            # Lowerbound time window for pickup
//    param upper_pickup {C} >= 0;            # Upper time window for pickup
//    param lower_delivery {C} >= 0;          # Lowerbound time window for delivery
//    param upper_delivery {C} >= 0;          # Upper time window for delivery

}
