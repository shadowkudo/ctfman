interface User {
	name: string;
	email?: string;
	role: {
		challenger?: boolean;
		admin?: boolean;
		moderator?: boolean;
		author?: boolean;
	};
	createdAt?: Date;
	deletedAt?: Date;
}

export type { User };
