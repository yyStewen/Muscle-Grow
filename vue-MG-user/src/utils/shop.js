export const parseDetailValues = (value) => {
  if (!value) return [];

  try {
    const parsed = JSON.parse(value);
    return Array.isArray(parsed) ? parsed.filter(Boolean) : [];
  } catch (error) {
    return [];
  }
};

export const normalizeSupplement = (product) => {
  const detailGroups = (product.details || [])
    .map((detail) => ({
      id: detail.id,
      name: detail.name,
      values: parseDetailValues(detail.value)
    }))
    .filter((detail) => detail.name && detail.values.length);

  return {
    ...product,
    detailGroups,
    hasSpecs: detailGroups.length > 0
  };
};

export const formatCurrency = (amount) => {
  return Number(amount || 0).toFixed(2);
};

export const buildSpecText = (specs) => {
  const entries = Object.entries(specs || {}).filter(([, value]) => value);
  return entries.map(([name, value]) => `${name}: ${value}`).join(' / ');
};

