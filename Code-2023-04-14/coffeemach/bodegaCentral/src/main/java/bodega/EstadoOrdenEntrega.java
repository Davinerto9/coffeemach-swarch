package bodega;

public enum EstadoOrdenEntrega {
    REGISTRADA,      // Order management: orden recibida y registrada
    EN_PICKING,      // Picking & packing: alistando materiales
    DESPACHADA,      // Outbound goods: materiales entregados al técnico
    RECIBIDA         // confirmación de recepción
}