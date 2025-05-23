package com.donorservice.model;

import com.donorservice.enums.StemCellType;
import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "stem_cell_donations")
public class StemCellDonation extends Donation {

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StemCellType stemCellType;

    @Column(nullable = false)
    private Double quantity;
}
