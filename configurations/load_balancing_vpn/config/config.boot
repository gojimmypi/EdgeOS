firewall {
    all-ping enable
    broadcast-ping disable
    ipv6-receive-redirects disable
    ipv6-src-route disable
    ip-src-route disable
    log-martians disable
    group {
        network-group PRIVATE_NETS {
            network 192.168.0.0/16
            network 172.16.0.0/12
            network 10.0.0.0/8
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
            state {
                established enable
                related enable
            }
            description "Allow established/related"
        }
        rule 20 {
            action drop
            state {
                invalid enable
            }
            description "Drop invalid state"
        }
    }
    name WAN_LOCAL {
        default-action drop
        description "WAN to router"
        rule 10 {
            action accept
            state {
                established enable
                related enable
            }
            description "Allow established/related"
        }
        rule 20 {
            action drop
            state {
                invalid enable
            }
            description "Drop invalid state"
        }
    }
    receive-redirects disable
    send-redirects enable
    source-validation disable
    syn-cookies enable
}
interfaces {
    ethernet eth0 {
        duplex auto
        speed auto
        description WAN
        address dhcp
        firewall {
            in {
                name WAN_IN
            }
            local {
                name WAN_LOCAL
            }
        }
    }
    ethernet eth1 {
        duplex auto
        speed auto
        description "WAN 2"
        address dhcp
        firewall {
            in {
                name WAN_IN
            }
            local {
                name WAN_LOCAL
            }
        }
    }
    ethernet eth2 {
        duplex auto
        speed auto
    }
    ethernet eth3 {
        duplex auto
        speed auto
    }
    ethernet eth4 {
        speed auto
        duplex auto
        poe {
            output off
        }
    }
    loopback lo {
    }
    switch switch0 {
        address 192.168.170.1/24
        description Local
        firewall {
            in {
                modify balance
            }
        }
        switch-port {
            interface eth2
            interface eth3
            interface eth4
        }
    }
}
load-balance {
    group G {
        interface eth0 {
        }
        interface eth1 {
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
    }
    dns {
        forwarding {
            cache-size 150
            listen-on switch0
        }
    }
    gui {
        https-port 443
    }
    nat {
        rule 5000 {
            outbound-interface eth0
            type masquerade
            description "masquerade for WAN"
        }
        rule 5002 {
            outbound-interface eth1
            type masquerade
            description "masquerade for WAN 2"
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
    crash-handler {
        send-crash-report false
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
