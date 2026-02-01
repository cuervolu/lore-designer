import dayjs from 'dayjs';
import relativeTime from 'dayjs/plugin/relativeTime';

dayjs.extend(relativeTime);

export function formatTimestamp(timestamp: number): string {
  const date = dayjs(timestamp * 1000);
  const now = dayjs();
  const daysDiff = now.diff(date, 'day');

  if (daysDiff < 7) {
    return date.fromNow();
  } else {
    return date.format('MMMM D, YYYY');
  }
}

export function formatFullTimestamp(timestamp: number): string {
  return dayjs(timestamp * 1000).format('MMMM D, YYYY [at] h:mm A');
}
