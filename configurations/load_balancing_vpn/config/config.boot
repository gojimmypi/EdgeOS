firewall {
    all-ping enable
    broadcast-ping disable
    group {
        network-group PRIVATE_NETS {
            network 192.168.0.0/16
            network 172.16.0.0/12
            network 10.0.0.0/8
        }
    }
    ipv6-receive-redirects disable
    ipv6-src-route disable
    ip-src-route disable
    log-martians disable
    modify SOURCE_ROUTE {
        rule 10 {
            action modify
            description "traffic from 192.168.170.0/24 to vtun0"
            modify {
                table 1
            }
            source {
                address 192.168.170.0/24
            }
        }
    }
    modify balance {
        rule 10 {
            action modify
            description "do NOT load balance lan to lan"
            destination {
                group {
                    network-group PRIVATE_NETS
                }
            }
            modify {
                table main
            }
        }
        rule 20 {
            action modify
            description "do NOT load balance destination public address"
            destination {
                group {
                    address-group ADDRv4_eth0
                }
            }
            modify {
                table main
            }
        }
        rule 30 {
            action modify
            description "do NOT load balance destination public address"
            destination {
                group {
                    address-group ADDRv4_eth1
                }
            }
            modify {
                table main
            }
        }
        rule 70 {
            action modify
            modify {
                lb-group G
            }
        }
    }
    name WAN_IN {
        default-action drop
        description "WAN to internal"
        rule 10 {
            action accept
            description "Allow established/related"
            state {
                established enable
                related enable
            }
        }
        rule 20 {
            action drop
            description "Drop invalid state"
            state {
                invalid enable
            }
        }
    }
    name WAN_LOCAL {
        default-action drop
        description "WAN to router"
        rule 10 {
            action accept
            description "Allow established/related"
            state {
                established enable
                related enable
            }
        }
        rule 20 {
            action drop
            description "Drop invalid state"
            state {
                invalid enable
            }
        }
    }
    receive-redirects disable
    send-redirects enable
    source-validation disable
    syn-cookies enable
}
interfaces {
    ethernet eth0 {
        address dhcp
        description WAN
        duplex auto
        firewall {
            in {
                name WAN_IN
            }
            local {
                name WAN_LOCAL
            }
        }
        speed auto
    }
    ethernet eth1 {
        address dhcp
        description "WAN 2"
        duplex auto
        firewall {
            in {
                name WAN_IN
            }
            local {
                name WAN_LOCAL
            }
        }
        speed auto
    }
    ethernet eth2 {
        duplex auto
        firewall {
            in {
                modify SOURCE_ROUTE
            }
        }
        speed auto
    }
    ethernet eth3 {
        duplex auto
        firewall {
            in {
                modify SOURCE_ROUTE
            }
        }
        speed auto
    }
    ethernet eth4 {
        duplex auto
        firewall {
            in {
                modify SOURCE_ROUTE
            }
        }
        poe {
            output off
        }
        speed auto
    }
    loopback lo {
    }
    openvpn vtun0 {
        config-file /config/openvpn/ca1098.nordvpn.com.udp.ovpn
        description "OpenVPN VPN tunnel"
    }
    switch switch0 {
        address 192.168.170.1/24
        description Local
        firewall {
            in {
                modify SOURCE_ROUTE
            }
        }
        mtu 1500
        switch-port {
            interface eth2 {
            }
            interface eth3 {
            }
            interface eth4 {
            }
            vlan-aware disable
        }
    }
}
load-balance {
    group G {
        exclude-local-dns disable
        flush-on-active enable
        gateway-update-interval 20
        interface eth0 {
        }
        interface eth1 {
        }
        lb-local enable
        lb-local-metric-change disable
    }
}
protocols {
    static {
        table 1 {
            interface-route 0.0.0.0/0 {
                next-hop-interface vtun0 {
                }
            }
        }
    }
}
service {
    dhcp-server {
        disabled false
        hostfile-update disable
        shared-network-name LAN {
            authoritative enable
            subnet 192.168.170.0/24 {
                default-router 192.168.170.1
                dns-server 192.168.170.1
                lease 86400
                start 192.168.170.38 {
                    stop 192.168.170.243
                }
            }
        }
        static-arp disable
        use-dnsmasq disable
    }
    dns {
        forwarding {
            cache-size 150
            listen-on switch0
        }
    }
    gui {
        http-port 80
        https-port 443
        older-ciphers enable
    }
    nat {
        rule 5000 {
            description "masquerade for WAN"
            outbound-interface eth0
            type masquerade
        }
        rule 5002 {
            description "masquerade for WAN 2"
            outbound-interface eth1
            type masquerade
        }
        rule 5100 {
            description "OpenVPN Clients"
            log disable
            outbound-interface vtun0
            source {
                address 192.168.170.0/24
            }
            type masquerade
        }
    }
    ssh {
        port 22
        protocol-version v2
    }
    unms {
    }
}
system {
    analytics-handler {
        send-analytics-report false
    }
    conntrack {
        expect-table-size 4096
        hash-size 4096
        table-size 32768
        tcp {
            half-open-connections 512
            loose enable
            max-retrans 3
        }
    }
    crash-handler {
        send-crash-report false
    }
    host-name EdgeRouter-X-5-Port
    login {
        user ubnt {
            authentication {
                encrypted-password $1$zKNoUbAo$gomzUbYvgyUMcD436Wo66.
            }
            level admin
        }
    }
    ntp {
        server 0.ubnt.pool.ntp.org {
        }
        server 1.ubnt.pool.ntp.org {
        }
        server 2.ubnt.pool.ntp.org {
        }
        server 3.ubnt.pool.ntp.org {
        }
    }
    syslog {
        global {
            facility all {
                level notice
            }
            facility protocols {
                level debug
            }
        }
    }
    time-zone UTC
}


/* Warning: Do not remove the following line. */
/* === vyatta-config-version: "config-management@1:conntrack@1:cron@1:dhcp-relay@1:dhcp-server@4:firewall@5:ipsec@5:nat@3:qos@1:quagga@2:suspend@1:system@5:ubnt-l2tp@1:ubnt-pptp@1:ubnt-udapi-server@1:ubnt-unms@2:ubnt-util@1:vrrp@1:vyatta-netflow@1:webgui@1:webproxy@1:zone-policy@1" === */
/* Release version: v2.0.9-hotfix.1.5371034.210122.1014 */
