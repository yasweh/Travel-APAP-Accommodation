package apap.ti._5.accommodation_2306212083_be.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "property")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Property {

    @Id
    @Column(name = "property_id", length = 50)
    private String propertyId;

    @Column(name = "property_name", nullable = false)
    private String propertyName;

    @Column(name = "type", nullable = false)
    private Integer type; // 0=Hotel, 1=Villa, 2=Apartemen

    @Column(name = "address", nullable = false, columnDefinition = "TEXT")
    private String address;

    @Column(name = "province", nullable = false)
    private Integer province;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "total_room", nullable = false)
    @Builder.Default
    private Integer totalRoom = 0;

    @Column(name = "active_status", nullable = false)
    @Builder.Default
    private Integer activeStatus = 1; // 0=Inactive, 1=Active

    @Column(name = "active_room", nullable = false)
    @Builder.Default
    private Integer activeRoom = 1; // 0=Inactive, 1=Active

    @Column(name = "income", nullable = false)
    @Builder.Default
    private Integer income = 0;

    @Column(name = "owner_name", nullable = false)
    private String ownerName;

    @Column(name = "owner_id", nullable = false)
    private UUID ownerId;

    @Column(name = "created_date", nullable = false)
    private LocalDateTime createdDate;

    @Column(name = "updated_date")
    private LocalDateTime updatedDate;

    // Relations
    @OneToMany(mappedBy = "property", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @JsonIgnore
    private List<RoomType> listRoomType = new ArrayList<>();

    @PrePersist
    public void prePersist() {
        this.createdDate = LocalDateTime.now();
        this.updatedDate = LocalDateTime.now();
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedDate = LocalDateTime.now();
    }

    public String getTypeString() {
        switch (this.type) {
            case 0: return "Hotel";
            case 1: return "Villa";
            case 2: return "Apartemen";
            default: return "Unknown";
        }
    }

    public String getActiveStatusString() {
        return this.activeStatus == 1 ? "Active" : "Inactive";
    }

    // Method to calculate actual room count from room types
    public int getActualRoomCount() {
        if (listRoomType == null || listRoomType.isEmpty()) {
            return 0;
        }
        return listRoomType.stream()
            .mapToInt(roomType -> roomType.getListRoom() != null ? roomType.getListRoom().size() : 0)
            .sum();
    }
}
