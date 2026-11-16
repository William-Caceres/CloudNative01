export function formatearPrecio(valor) {
  return new Intl.NumberFormat('es-CL').format(valor)
}
