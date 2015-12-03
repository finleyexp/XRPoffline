#include <string>
#include <vector>
#include <QFile>
#include <QString>
#include <QMessageBox>
#include <QTextStream>
#include "data.h"
#include "mainwindow.h"
#include <ripple.h>
#include <QJsonObject>
#include <QJsonArray>
#include <QJsonDocument>
#include "aesCrypter.h"

#ifndef WIN32
#include <sys/types.h>
#include <sys/stat.h>
#include <unistd.h>
#endif

int AccountData::itemIdCounter = 0;

Wallet* Wallet::wallet = NULL;


int AccountData::Init(const QString& _name, const QString& _addr, const QString& _key, int _sequence)
{
	itemId = itemIdCounter;
	itemIdCounter++;
	name = _name;
	addr = _addr;
	key = _key;
	sequence = _sequence;

    canReceive = rippleValidateAddress(addr.toStdString());
    if (canReceive)
    {
        canSend = rippleGetAddressFromSecret(key.toStdString())==addr.toStdString();
    }
    else canSend = false;



	return itemId;
}


bool AccountData::LoadFromJson(QJsonObject &json, AEScrypter* crypter)
{
	QString nm = json["name"].toString();
	QString ky = json["keyEncrypted"].toString();
	QString ad = json["address"].toString();
	int seq;
	
	if (ky.isEmpty())
	{		
		ad = json["address"].toString();
		if (!rippleValidateAddress(ad.toStdString()))
			throw std::exception( QString("Invalid ripple address for account " + nm).toLocal8Bit().data() );
	}
	else
	{	
		seq = json["sequence"].toInt();
		if (seq <= 0)
			throw std::exception(QString("Invalid sequence for account " + nm).toLocal8Bit().data());
		
		std::string seedHex = crypter->decrypt(ky.toStdString());
		std::string secret = getAccountSecretFromSeed(seedHex);

		std::string stdAddr = rippleGetAddressFromSecret(secret);
		if (stdAddr.empty())
			throw std::exception(QString("Failed to decrypt secret for account " + nm).toLocal8Bit().data());
		if (stdAddr!=ad.toStdString())
			throw std::exception(QString("Password failed when loading account " + nm).toLocal8Bit().data());
		ky = QString(secret.c_str());
	}
	Init(nm, ad, ky, seq);
	return true;
}

void AccountData::SaveToJson(QJsonObject &json, AEScrypter* crypter)
{
	json["name"] = name;
	if (key.isEmpty())
		json["address"] = addr;
	else
	{
		std::string seed = getAccountSeed(key.toStdString());		
		json["keyEncrypted"] = QString(crypter->encrypt(seed).c_str());
		json["address"] = addr;
		json["sequence"] = sequence;
	}
}


#ifdef WIN32
#include <windows.h>
#include <Shlobj.h>
static std::string GetSpecialFolderPath(int nFolder, bool fCreate = true)
{
	char pszPath[MAX_PATH] = "";

	if (SHGetSpecialFolderPathA(NULL, pszPath, nFolder, fCreate))
	{
		return pszPath;
	}
	return "";
}
#endif


/*static*/ Wallet* Wallet::GetWallet()
{
	if (!wallet)
		wallet = new Wallet();
	return wallet;
}

Wallet::Wallet() 
{
	crypter = new AEScrypter();
}

Wallet::~Wallet() { delete crypter; }

void Wallet::SetNewPassword(const QString& password)
{ 	
	this->password = password.toStdString(); 
    saltHex = crypter->GenerateSalt();
    crypter->SetPassword(this->password, saltHex, 100);
}

QString Wallet::SaveAccounts() 
{ 
	if (saltHex.empty())
		throw std::exception("Can not save wallet, salt not set.");

	QJsonObject json;
	QJsonArray accountsJsonArray;

	std::vector<AccountData>::iterator it = accounts.begin();
	it++;
	for (; it != accounts.end(); it++)
	{
		AccountData& i = *it;

		QJsonObject accountJson;
		i.SaveToJson(accountJson, crypter);
		accountsJsonArray.append(accountJson);
	}

	json["version"] = 1;
	json["salt"] = QString(saltHex.c_str());
	json["accounts"] = accountsJsonArray;
	json["passHashEnc"] = QString(crypter->encrypt(crypter->getPasswordHash(password)).c_str());
	
	QJsonDocument saveDoc(json);

	return saveDoc.toJson(QJsonDocument::Indented);
}

std::vector<int> Wallet::LoadFromJson(const QString& jsonString, const QString& password)
{	
	QJsonDocument loadDoc(QJsonDocument::fromJson(jsonString.toUtf8()));

	saltHex = loadDoc.object()["salt"].toString().toStdString();
	if (saltHex.empty())
		throw std::exception("Missing salt in wallet file");
	
	if (crypter->SetPassword(password.toStdString(), saltHex, 100) == false)
		throw std::exception("Failed to set password & salt");

	QString phash = crypter->encrypt(crypter->getPasswordHash(password.toStdString())).c_str();
	QString passHashEnc = loadDoc.object()["passHashEnc"].toString();
	if (phash!=passHashEnc)
		throw std::exception("Incorrect password.");

	this->password = password.toStdString();

	std::vector<AccountData> accountsTemp;

	QJsonArray accountArray = loadDoc.object()["accounts"].toArray();
	for (int i = 0; i < accountArray.size(); i++)
	{
		AccountData d;
		d.LoadFromJson(accountArray[i].toObject(), crypter);
		accountsTemp.push_back(d);
	}

	std::vector<int> result;
	for (size_t i = 0; i < accountsTemp.size(); i++)
	{
		result.push_back( AddAccount(accountsTemp[i]) );
	}

	return result; 
}

int Wallet::AddAccount(AccountData& newAccount)
{
	accounts.push_back(newAccount);

	return newAccount.GetId();
}

AccountData* Wallet::GetAccount(int accountId)
{
	std::vector<AccountData>::iterator it = accounts.begin();
	for (; it != accounts.end(); it++)
	{
		if (it->GetId() == accountId)
			return &*it;
	}
	return NULL;
}

void Wallet::DeleteAccount(int accountId)
{
	std::vector<AccountData>::iterator it = accounts.begin();
	for (; it != accounts.end(); it++)
	{
		if (it->GetId() == accountId)
		{
			accounts.erase(it);
			return;
		}
	}
}
