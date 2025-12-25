export enum TicketPriority {
  CRITICAL = 'critical',
  HIGH = 'high',
  MEDIUM = 'medium',
  LOW = 'low'
}

export enum TicketStatus {
  NEW = 'new',
  IN_PROGRESS = 'in_progress',
  WAITING_CUSTOMER = 'waiting_customer',
  RESOLVED = 'resolved',
  CLOSED = 'closed'
}

export enum TicketSource {
  WEB = 'web',
  EMAIL = 'email',
  API = 'api',
  AWS_SUPPORT = 'aws_support',
  AWS_HEALTH = 'aws_health'
}

export interface Ticket {
  id: string;
  customer_id: string;
  title: string;
  description: string;
  priority: TicketPriority;
  status: TicketStatus;
  source: TicketSource;
  aws_case_id?: string;
  aws_health_event_arn?: string;
  assigned_to?: string;
  created_at: Date;
  updated_at: Date;
  sla_due_at: Date;
  resolved_at?: Date;
  closed_at?: Date;
}

export interface TicketComment {
  id: string;
  ticket_id: string;
  user_id: string;
  content: string;
  is_internal: boolean;
  created_at: Date;
}
