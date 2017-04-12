package org.egov.persistence.entity;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "notification")
@SequenceGenerator(name = Notification.SEQ_NOTIFICATION, sequenceName = Notification.SEQ_NOTIFICATION, allocationSize = 1)
public class Notification extends AbstractAuditable {

    public static final String SEQ_NOTIFICATION = "SEQ_NOTIFICATION";

    @Id
    @GeneratedValue(generator = Notification.SEQ_NOTIFICATION, strategy = GenerationType.SEQUENCE)
    private Long id;

    @Column(name = "messagecode")
    private String messageCode;

    @Column(name = "messagevalues")
    private String messageValues;

    @Column(name = "reference")
    private String reference;

    @Column(name = "userid")
    private Long userId;

    @Column(name = "read")
    private String read;

}