import { error } from '@sveltejs/kit';
import { type ClassValue, clsx } from 'clsx';
import { twMerge } from 'tailwind-merge';

export function cn(...inputs: ClassValue[]) {
	return twMerge(clsx(inputs));
}

enum Options {
	UNAUTHENTICATED = 401,
	NOT_FOUND = 404,
	FORBIDDEN = 403
}

type ErrType = `${Options}` extends `${infer T extends number}` ? T : never;

const errorBody: Record<Options, App.Error> = {
	401: {
		message: 'Unauthenticated',
		subtext: 'You need to login to access this content'
	},
	404: {
		message: 'Not found',
		subtext: 'Sorry, we couldn’t find the page you’re looking for.'
	},
	403: {
		message: 'Forbidden',
		subtext: "You aren't allowed to do that :)"
	}
};

/**
 * Common errors with prefilled body
 */
export function useError(err: ErrType): never {
	error(err, errorBody[err]);
}
