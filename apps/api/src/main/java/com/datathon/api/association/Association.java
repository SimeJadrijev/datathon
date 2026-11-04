package com.datathon.api.association;

import com.datathon.api.fundingcall.Area;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.util.HashSet;
import java.util.Set;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "association", uniqueConstraints = @UniqueConstraint(name = "uk_association_oib", columnNames = "oib"))
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Association {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 11)
    private String oib;

    @Column(nullable = false, length = 1000)
    private String name;

    @Column(length = 500)
    private String seat;

    @Column(length = 100)
    private String county;

    @Column(length = 20)
    private String registryNumber;

    @ElementCollection
    @CollectionTable(name = "association_area", joinColumns = @JoinColumn(name = "association_id"))
    @Enumerated(EnumType.STRING)
    @Column(name = "area", length = 30)
    private Set<Area> areas = new HashSet<>();

    public Association(String oib, String name) {
        this.oib = oib;
        this.name = name;
    }

    public String municipality() {
        if (seat == null || !seat.contains(",")) {
            return seat;
        }
        return seat.substring(seat.lastIndexOf(',') + 1).strip();
    }
}
