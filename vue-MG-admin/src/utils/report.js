export const formatDate = (date) => {
  const year = date.getFullYear();
  const month = `${date.getMonth() + 1}`.padStart(2, '0');
  const day = `${date.getDate()}`.padStart(2, '0');
  return `${year}-${month}-${day}`;
};

export const getRecentDaysRange = (days) => {
  const end = new Date();
  const begin = new Date();
  begin.setDate(end.getDate() - days + 1);
  return [formatDate(begin), formatDate(end)];
};

export const parseCsvList = (value, mapper = (item) => item) => {
  if (!value) {
    return [];
  }

  return String(value)
    .split(',')
    .map((item) => item.trim())
    .filter(Boolean)
    .map(mapper);
};

export const toNumber = (value) => {
  const parsed = Number(value);
  return Number.isFinite(parsed) ? parsed : 0;
};

export const buildDateShortcuts = () => [
  {
    text: '近7天',
    value: () => getRecentDaysRange(7)
  },
  {
    text: '近30天',
    value: () => getRecentDaysRange(30)
  }
];
